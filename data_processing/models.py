import os
import glob
import csv
import pandas as pd
import numpy as np
from collections import deque
from itertools import chain
from utils import rotate_quat, rotate_cross_product


class Sensor(object):
    def __init__(self, name, fieldnames, data):
        self.name = name
        self.fieldnames = fieldnames
        self.raw_data = data
        self.length = data[-1, 0] - data[0, 0]

    def __str__(self):
        return '<Sensor "{}">'.format(self.name)

    def __repr__(self):
        return str(self)

    def __getitem__(self, key):
        if isinstance(key, tuple):
            keys = [self.fieldnames.index(k) for k in key]
        else:
            keys = self.fieldnames.index(key)
        return self.raw_data[:, keys]


class Recording(object):
    def __init__(self, path, step_stride_threshold=300):
        self.path = path
        self.sensors = []
        self.subject = 'unknown'
        sensors_i = (p for p in glob.iglob(os.path.join(path, '*.csv')) if '-extra' not in p)
        for sensor_log in sensors_i:
            sensor_name = os.path.splitext(os.path.split(sensor_log)[-1])[0]
            with open(sensor_log) as f:
                reader = csv.reader(f)
                try:
                    fieldnames = next(reader)
                except StopIteration:
                    continue
                data = np.array([self._parse_line(fieldnames, l) for l in reader])
                try:
                    sensor = Sensor(sensor_name, fieldnames, data)
                except IndexError:
                    print('Error: Empty sensor {}'.format(sensor_log))
                else:
                    setattr(self, sensor_name, sensor)
                    self.sensors.append(sensor)

        if self.foot_sensors_available:
            self.filter_steps(step_stride_threshold)
        else:
            print('Warning: Not all foot sensors available')

        with open(os.path.join(path, 'meta.txt')) as f:
            lines = f.readlines()
            for l in lines:
                if l.startswith('now'):
                    self.recording_start = int(l.split(' ')[-1]) * 1e-9
                elif l.startswith('name'):
                    self.subject = l.split(' ')[-1].strip()

    def _parse_line(self, fieldnames, l):
        assert len(fieldnames) == len(l), f'_parse_line({fieldnames}, {l})'
        for i in range(len(l)):
            if fieldnames[i].endswith('_ts'):
                value = int(l[i]) * 1e-9
            elif fieldnames[i].startswith('is_') or fieldnames[i] == 'running':
                value = l[i] == 'true'
            else:
                value = float(l[i])
            l[i] = value
        return l

    def __str__(self):
        return '<{} "{}" raw-sensors={}>'.format(
            os.path.split(self.path)[-1],
            self.condition,
            [s.name for s in self.sensors])

    def __repr__(self):
        return str(self)

    def write(self, file_name):
        # sensors = {s.name: s.raw_data for s in self.sensors}
        # sensors['merged'] = self.merged
        # np.savez_compressed(os.path.join(self.path, file_name),
        #                     **sensors)
        self.merged.to_msgpack(os.path.join(self.path, file_name))

    def merge(self, step_margin=1.):
        data_sensors = self.data_sensors
        arrivals = [s['arrival_ts'] for s in data_sensors]
        idc = [0] * len(data_sensors)
        qs = [deque(maxlen=1) for _ in data_sensors]
        result = deque()
        try:
            while True:
                # fill queues with newest data point
                new_arr = [s[idc[i]] for i, s in enumerate(arrivals)]
                newest = np.argmin(new_arr)
                qs[newest].append(data_sensors[newest].raw_data[idc[newest]])
                idc[newest] += 1

                # check if all queues contain data
                if all([len(q) > 0 for q in qs]):
                    # create new data point containing all sensor data
                    # assign average timestamp
                    avg_timestamp = np.mean([q[0][0] for q in qs])
                    label = self.label_for_timestamp(avg_timestamp, step_margin)

                    data = [avg_timestamp, label]

                    # append sensor data: data fields [2:6]
                    data += list(chain(*(q.popleft()[2:6] for q in qs)))
                    result.append(data)

        except IndexError:
            pass

        cols = ['event_ts', 'label']
        for s in data_sensors:
            cols += ['{}_{}'.format(s.name, fn) for fn in s.fieldnames[2:6]]
        self.merged = pd.DataFrame.from_records(list(result), columns=cols)

    @property
    def data_sensors(self):
        label_sensor_names = ('RightFootSensor', 'LeftFootSensor')
        return [s for s in self.sensors if s.name not in label_sensor_names and '-extra' not in s.name]

    @property
    def foot_sensors_available(self):
        return hasattr(self, 'RightFootSensor') and hasattr(self, 'LeftFootSensor')

    def label_for_timestamp(self, timestamp, step_margin=1.):
        '''TODO DOCS'''
        if not self.foot_sensors_available:
            return False
        for s in (self.RightFootSensor, self.LeftFootSensor):
            arrivals = s['arrival_ts']
            ts_idx = np.searchsorted(arrivals, timestamp)
            step_durations = s['duration'] * 1e-3
            if ts_idx < step_durations.shape[0]:
                step_start_ts = arrivals[ts_idx] - step_durations[ts_idx] - step_margin
                # print(arrivals[ts_idx + 1], step_durations[ts_idx + 1], step_start_ts, timestamp)
                if timestamp >= step_start_ts:
                    # print('in')
                    # step_start_ts <= timestamp <= step_arrival
                    return True
        return False

    def rotate_accelerometer(self):
        acc = self.merged.as_matrix(['Accelerometer_val_x',
                                     'Accelerometer_val_y',
                                     'Accelerometer_val_z'])
        rot = self.merged.as_matrix(['GameRotationVector_val_w',
                                     'GameRotationVector_val_x',
                                     'GameRotationVector_val_y',
                                     'GameRotationVector_val_z'])
        if np.isnan(rot).any():
            print('WARNING: GameRotationVector data unavailable. Fallback to RotationVector.')
            rot = self.merged.as_matrix(['RotationVector_val_w',
                                         'RotationVector_val_x',
                                         'RotationVector_val_y',
                                         'RotationVector_val_z'])
        if np.isnan(rot).any():
            raise ValueError('No RotationVector data available. Cannot rotate accelerometer.')
        keys = 'Rotated_Accelerometer_val_x', 'Rotated_Accelerometer_val_y', 'Rotated_Accelerometer_val_z'
        rot_acc = rotate_quat(acc, rot)
        self.merged = self.merged.assign(**{keys[i]: rot_acc[:, i] for i in range(len(keys))})

    def normalize_accelerometer(self, norm_reference):
        acc = self.merged.as_matrix(['Accelerometer_val_x',
                                     'Accelerometer_val_y',
                                     'Accelerometer_val_z'])
        rot = self.merged.as_matrix(['GameRotationVector_val_w',
                                     'GameRotationVector_val_x',
                                     'GameRotationVector_val_y',
                                     'GameRotationVector_val_z'])
        if np.isnan(rot).any():
            print('WARNING: GameRotationVector data unavailable. Fallback to RotationVector.')
            rot = self.merged.as_matrix(['RotationVector_val_w',
                                         'RotationVector_val_x',
                                         'RotationVector_val_y',
                                         'RotationVector_val_z'])
        if np.isnan(rot).any():
            raise ValueError('No RotationVector data available. Cannot rotate accelerometer.')
        keys = 'Normalized_Accelerometer_val_x', 'Normalized_Accelerometer_val_y', 'Normalized_Accelerometer_val_z'
        rot_acc = rotate_quat(acc, rot)
        rot_acc[:, 2] -= 9.8
        rot_acc /= norm_reference
        self.merged = self.merged.assign(**{keys[i]: rot_acc[:, i] for i in range(len(keys))})

    def filter_steps(self, th):
        for s in (self.RightFootSensor, self.LeftFootSensor):
            step_data = s['stride_x', 'stride_y']
            step_norm = np.linalg.norm(step_data, axis=1)
            print('{}: {} steps detected as too small'.format(s.name, np.sum(step_norm < th)))
            s.raw_data = s.raw_data[step_norm >= th]
