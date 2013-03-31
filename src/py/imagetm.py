# -*- coding: utf-8 -*-
#!/usr/bin/python
# author: leo
# data: 2012-09-25

import urllib, urllib2, sys, os, json, time, datetime, threading

USAGE = '>> usage: imagetm <image-url-list-file> <image-save-path> <thread-num>'
WGET_PARAMS = '--user-agent="%s" --header="%s" --header="%s" --header="%s" --header="%s" --header="%s"'\
    % ("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6",
      "Accept:text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
      "Accept-Encoding: gzip,deflate"
      "Accept-Language: en-us,en;q=0.5",
      "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7",
      "Keep-Alive: 300")
  
  
# a image retrieval worker
# a url_desc looks like:
#    黑鱼片 http://1.2.3/xx.jpg    400x366    雪菜黑鱼片(图)_吃四方美食天地    雪菜黑鱼片
def worker(pid, url_descs, path):
    count = 0 
    cur_count = 0
    cur_name = ''
    begin_time = datetime.datetime.now();
    for url in url_descs:
        desc = url.split('\t')
        if len(desc) != 5:
            print 'pid.', pid, 'error format of ' + desc
            continue
        # get sub of pic ,such as .jpg
        url = desc[1]
        sub = url.split('/')[-1]
        point_pos = sub.find('.')
        if point_pos == -1:
            sub = '.jpg'
        else:
            sub = sub[point_pos:]
        # get current sub name
        if cur_name != desc[0]:
            cur_name = desc[0]
            cur_count = 0
        # join full local image path
        file_name = path + '/' + cur_name + '-p' + str(pid) + "." + str(cur_count) + sub;
        # get the image
        time = datetime.datetime.now() - begin_time;
        print '\n>> pid-' + str(pid) + '.' + str(count) + '.\t time:' + str(time)\
                + '\t Downloading [' + url + '] to [' + file_name + ']...'
        retry_count = 0;
        for retry_count in range(5):
            try:
                os.system('wget %s -O %s -T 5 -t 3 ' %(url, file_name))
                if os.stat(file_name).st_size == 0:
                    retry_count = retry_count + 1
                    print '  | pid-' + str(pid) + '.' + str(count)\
                            + ': get nothing of ' + url + ' --> ' + file_name + ', retry', retry_count
                    continue
                else:
                   break 
                # urllib.urlretrieve(url, file_name)
            except Exception, e:
                print '  | pid-' + str(pid) + '.' + str(count)\
                        + ': unknown error of ' + url + ' --> ' + file_name + ': ', e
        cur_count += 1
        count += 1

# get all images in <image-url-list-file> into <image-save-path> use <thread-num> threads.
def run():
    print USAGE
    # 1. get the args
    if len(sys.argv) != 4:
        return
    path = sys.argv[2]
    image_url_descs = open(sys.argv[1]).readlines()
    nworker = int(sys.argv[3])
    
    # 2. dispatch all urls to all workers.
    worker_urls = []
    for i in range(0, nworker):
        worker_urls.append([])
    count = 0
    for desc in image_url_descs:
        worker_urls[count % nworker].append(desc)
        count = count + 1 

    time = datetime.datetime.now();
    worker_threads = []
    for i in range(0, nworker):
        thread = threading.Thread(target = worker, args = (i, worker_urls[i], path))
        worker_threads.append(thread)
        thread.start()

    for i in range(0, nworker):
        threading.Thread.join(worker_threads[i])
    time = datetime.datetime.now() - time
    print 'all: [', time, '] seconds.'
run()
