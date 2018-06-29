#!/usr/bin/python2.7
# -*- coding: utf-8 -*-

import caffe

from PIL import ImageFile
ImageFile.LOAD_TRUNCATED_IMAGES = True

MODEL_ALEX_DIR = 'AlexNet/'
MODEL_ALEX_FILE = MODEL_ALEX_DIR + 'oxford102.caffemodel'
DEPLOY_ALEX_FILE = MODEL_ALEX_DIR + 'deploy.prototxt'

MODEL_RES_DIR = 'ResNet/'
MODEL_RES_FILE = MODEL_RES_DIR + 'ResNet-152.caffemodel'
DEPLOY_RES_FILE = MODEL_RES_DIR + 'deploy.prototxt'

RES_OFFSET = 110

REPREDICT_THRESHOLD = 0.7


def init_model():
    caffe.set_mode_cpu()

    alex_net = caffe.Net(DEPLOY_ALEX_FILE, MODEL_ALEX_FILE, caffe.TEST)

    alex_transformer = caffe.io.Transformer(
        {'data': alex_net.blobs['data'].data.shape})

    # python读取的图片文件格式为H×W×K，需转化为K×H×W
    alex_transformer.set_transpose('data', (2, 0, 1))

    # python中将图片存储为[0, 1]，而caffe中将图片存储为[0, 255]，
    # 所以需要一个转换
    alex_transformer.set_raw_scale('data', 255)

    # caffe中图片是BGR格式，而原始格式是RGB，所以要转化
    alex_transformer.set_channel_swap('data', (2, 1, 0))

    # 将输入图片格式转化为合适格式（与deploy文件相同）
    # alex_net.blobs['data'].reshape(1, 3, 227, 227)

    res_net = caffe.Net(DEPLOY_RES_FILE, MODEL_RES_FILE, caffe.TEST)

    res_transformer = caffe.io.Transformer(
        {'data': res_net.blobs['data'].data.shape})

    # python读取的图片文件格式为H×W×K，需转化为K×H×W
    res_transformer.set_transpose('data', (2, 0, 1))

    # python中将图片存储为[0, 1]，而caffe中将图片存储为[0, 255]，
    # 所以需要一个转换
    res_transformer.set_raw_scale('data', 255)

    # caffe中图片是BGR格式，而原始格式是RGB，所以要转化
    res_transformer.set_channel_swap('data', (2, 1, 0))

    # 将输入图片格式转化为合适格式（与deploy文件相同）
    # res_net.blobs['data'].reshape(1, 3, 224, 224)

    return alex_net, alex_transformer, res_net, res_transformer


def classify_image(image_name, alex_net, alex_transformer, res_net, res_transformer):

    # 详见/caffe/python/caffe/io.py
    img = caffe.io.load_image(image_name)

    # 数据输入、预处理
    alex_net.blobs['data'].data[...] = alex_transformer.preprocess('data', img)
    alex_out = alex_net.forward()
    alex_results = alex_out['prob']
    alex_result = int(alex_results.argmax())
    alex_prob = float(alex_results.max())

    if alex_prob >= REPREDICT_THRESHOLD:
        result = alex_result
        prob = alex_prob
    else:
        res_net.blobs['data'].data[...] = res_transformer.preprocess(
            'data', img)
        res_out = res_net.forward()
        res_results = res_out['prob']
        res_result = int(res_results.argmax())
        res_prob = float(res_results.max())
        result = res_result + RES_OFFSET
        prob = res_prob

    return result, prob
