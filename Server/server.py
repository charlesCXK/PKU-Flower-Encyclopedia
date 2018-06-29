#-*- coding: UTF-8 -*-

import socket
import threading
import time
import sys
import os
import struct
import flower_classification as fc


SERVER_PORT = 8432
TEMP_DIR = 'temp/'
MAX_TEMP_SIZE = 100

INFO_HEADER = 'FC'
SERVER_RDY = 'FCRDY'
RESULT_DONE = 'FCDONE'


def socket_service():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind(('0.0.0.0', SERVER_PORT))
        s.listen(10)
    except socket.error as msg:
        print msg
        sys.exit(1)
    print 'Server initialized'

    alex_model, alex_transformer, res_model, res_transformer = fc.init_model()

    while 1:
        print 'Start listening'
        conn, addr = s.accept()
        # t = threading.Thread(target=deal_data, args=(conn, addr))
        # t.start()
        deal_data(conn, addr, alex_model, alex_transformer,
                  res_model, res_transformer)


def deal_data(conn, addr, alex_model, alex_transformer, res_model, res_transformer):
    print 'Accept new connection from {}'.format(addr)
    # conn.settimeout(500)

    while 1:
        buf = conn.recv(1024)
        print 'Get request: ' + buf
        if buf:
            strlist = buf.split('|')
            if len(strlist) == 4 and strlist[0] == INFO_HEADER:
                file_size = int(strlist[1])
                file_format = strlist[2]
                print '    filesize: ' + str(file_size)
                print '    file format: ' + file_format
            else:
                print '    unknown format'
                break

            if not os.path.exists(TEMP_DIR):
                os.makedirs(TEMP_DIR)
            files = os.listdir(TEMP_DIR)
            if len(files) >= MAX_TEMP_SIZE:
                while len(files >= MAX_TEMP_SIZE):
                    os.remove(TEMP_DIR + files[0])
                    files.pop(0)

            file_num = len(files)
            new_filename = time.strftime(
                '%m_%d_%H-%M-%S', time.localtime()) + '.' + file_format
            new_filepath = TEMP_DIR + new_filename

            recvd_size = 0  # 定义已接收文件的大小
            fp = open(TEMP_DIR + new_filename, 'wb')

            # conn.send(SERVER_RDY)
            print '    start receiving...'
            while not recvd_size == file_size:
                if file_size - recvd_size > 1024:
                    data = conn.recv(1024)
                    recvd_size += len(data)
                else:
                    data = conn.recv(file_size - recvd_size)
                    recvd_size = file_size

                fp.write(data)
            fp.close()

            print "    received: " + str(recvd_size)
            print '    end receive...'
            print '    save to ' + new_filepath

            result, prob = fc.classify_image(
                new_filepath, alex_model, alex_transformer, res_model, res_transformer)
            print '    result: ' + str(result) + ', prob: ' + str(prob)

            conn.send('FCRES|{}|'.format(result))
            conn.close()
            break


if __name__ == '__main__':
    socket_service()
