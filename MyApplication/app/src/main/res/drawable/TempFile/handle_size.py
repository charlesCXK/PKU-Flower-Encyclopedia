# http://www.nongmiao.com/baike/p2/
# https://baike.baidu.com/item/%E9%A3%8E%E4%BF%A1%E5%AD%90/22530

import os
from PIL import Image

dirs = os.walk('./')
for (nowdir, nextdir, file) in dirs:
    myfile = file
for file in myfile:
    if not ('jpg' in file or 'png' in file):
        continue
    img = Image.open('./'+file)
    out = img.resize((220, 165),Image.ANTIALIAS)
    out.save('./'+file)
