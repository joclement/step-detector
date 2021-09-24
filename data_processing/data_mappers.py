import os
import numpy as np
import pandas as pd
import tensorflow as tf


class Window_Creator(object):
    def __init__(self, rec_paths, train_test_split=.8, window_size=120,
                 subsample=1, uniform=True):
        self.window_size = window_size
        self.subsample = subsample
        self.stride = self.subsample - 1
        self.load_data(rec_paths)
        if 0. < train_test_split < 1.:
            self.split(train_test_split, uniform)
        else:
            all_idc = np.arange(self.labels.shape[0], dtype=int)
            split_idx = int(self.labels.shape[0] * train_test_split) + 1
            self.training_idc = all_idc[:split_idx]
            self.testing_idc = all_idc[split_idx:]

    def load_data(self, rec_paths):
        labels = []
        all_data = []
        for r in rec_paths:
            f = pd.read_msgpack(os.path.join(r, 'merged.msg'))
            for i in range(0, f.shape[0] - self.window_size):
                window = f.iloc[i + self.stride * i : i + self.stride * i + self.window_size : self.subsample]
                if window.shape[0] * self.subsample < self.window_size:
                    break
                labels.append(window['label'].mean())
                data = window.as_matrix(['Normalized_Accelerometer_val_x',
                                         'Normalized_Accelerometer_val_y',
                                         'Normalized_Accelerometer_val_z'])
                if np.isnan(data).any():
                    print('ERROR: Normalized Accelerometer is NaN. Ignoring', r)
                    break

        #             data = data.reshape(-1)
                all_data.append(data)
        self.accelerometer = np.array(all_data, dtype=np.float32)
        self.raw_labels = np.array(labels, dtype=np.float32)
        self.labels = np.round(self.raw_labels)

    def split(self, frac, uniform=True):
        np.random.seed(0)  # fixate seed for reproducable "random" results
        if uniform:
            # count labels
            num_walk = np.sum(self.labels == 1)
            num_not_walk = np.sum(self.labels == 0)
            if num_walk != num_not_walk:
                print('WARNING: Non-uniform data distribution: {} Walking vs {} Not-walking'.format(num_walk, num_not_walk))
                upper_limit = min(num_walk, num_not_walk)
            else:
                upper_limit = num_walk

            # extract idc by label
            idc_w = (self.labels == 1).nonzero()[0]
            idc_nw = (self.labels == 0).nonzero()[0]

            # randomize idc and cut off overflow
            rand_w = np.random.permutation(idc_w)[:upper_limit]
            rand_nw = np.random.permutation(idc_nw)[:upper_limit]

            # split by label
            split_idx = int(upper_limit * frac)
            train_w = rand_w[:split_idx]
            test_w = rand_w[split_idx:]
            train_nw = rand_nw[:split_idx]
            test_nw = rand_nw[split_idx:]

            # merge labels
            self.training_idc = np.random.permutation(np.append(train_w, train_nw))
            self.testing_idc = np.random.permutation(np.append(test_w, test_nw))
        else:
            # simple split, does not care about label distribution
            rand_idc = np.random.permutation(self.labels.shape[0])
            split_idx = int(self.labels.shape[0] * frac)
            self.training_idc = rand_idc[:split_idx]
            self.testing_idc = rand_idc[split_idx:]

    def window_input_fn(self, mode='training'):
        idc = self.training_idc if mode == 'training' else self.testing_idc
        return tf.estimator.inputs.numpy_input_fn(x={'accelerometer': self.accelerometer[idc]},
                                                  y=np.array(self.labels[idc], dtype=np.int32),
                                                  num_epochs=None, shuffle=True)

    def serving_input_fn(self):
        receiver_tensors = {'accelerometer': tf.placeholder(dtype=self.accelerometer.dtype,
                                                            shape=(None,) + self.accelerometer.shape[1:],
                                                            name='accelerometer_input')}
        return tf.estimator.export.ServingInputReceiver(receiver_tensors, receiver_tensors)


def randomized_input(shape, low=0, high=1, weights=None):
    np.random.seed(0)
    rand = np.random.rand(*shape).astype(np.float32)
    rand *= high - low
    rand -= low
    if weights is not None:
        rand *= weights[:, np.newaxis, np.newaxis]
    return tf.estimator.inputs.numpy_input_fn(x={'accelerometer': rand},
                                              batch_size=shape[0],
                                              num_epochs=1, shuffle=False)
