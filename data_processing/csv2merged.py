import sys
import os
import glob

from models import Recording

if __name__ == '__main__':
    record_name_pattern = sys.argv[-1]
    for record in glob.glob(record_name_pattern):
        print('Merging {}...'.format(record))
        r = Recording(os.path.abspath(record))
        r.merge(step_margin=.75)
        try:
            r.normalize_accelerometer(8.)
        except ValueError:
            print('ERROR: Accelerometer rotation unavailable for', r)
        else:
            r.write('merged.msg')
