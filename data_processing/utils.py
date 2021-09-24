'''
utils.py
'''

import glob
import os
import numpy as np


def enumerate_recording_paths(dataset_root):
    record_path_pattern = os.path.abspath(os.path.expanduser(dataset_root))
    for recording in glob.glob(record_path_pattern):
        if os.path.exists(os.path.join(recording, 'merged.msg')):
            yield recording


def find_closest(A, target):
    # thanks to https://stackoverflow.com/a/8929827
    idx = A.searchsorted(target)
    idx = np.clip(idx, 1, len(A) - 1)
    left = A[idx - 1]
    right = A[idx]
    idx -= target - left < right - target
    return idx


def rotate_cross_product(vectors, quats):
    t = np.cross(2 * -quats[:, 1:], vectors)
    return vectors + quats[:, :1] * t + np.cross(-quats[:, 1:], t)


def rotate_quat(vectors, quats):
    vecs = np.zeros(quats.shape)
    vecs[:, 1:] = vectors
    return q_mult(q_mult(quats, vecs), q_conj(quats))[:, 1:]


def q_mult(q1, q2):
    q3 = np.empty(q1.shape)
    q3[:, 0] = q1[:, 0] * q2[:, 0] - q1[:, 1] * q2[:, 1] - q1[:, 2] * q2[:, 2] - q1[:, 3] * q2[:, 3]
    q3[:, 1] = q1[:, 0] * q2[:, 1] + q1[:, 1] * q2[:, 0] + q1[:, 2] * q2[:, 3] - q1[:, 3] * q2[:, 2]
    q3[:, 2] = q1[:, 0] * q2[:, 2] + q1[:, 2] * q2[:, 0] + q1[:, 3] * q2[:, 1] - q1[:, 1] * q2[:, 3]
    q3[:, 3] = q1[:, 0] * q2[:, 3] + q1[:, 3] * q2[:, 0] + q1[:, 1] * q2[:, 2] - q1[:, 2] * q2[:, 1]
    return q3


def q_conj(q1):
    q2 = q1.copy()
    q2[:, 1:] *= -1
    return q2
