# -*- coding: utf-8 -*-
#!/usr/bin/python
# author: leo
# data: 2012-09-25

import urllib, urllib2, sys, os, json, time

USAGE = '>> usage: imaget <image-url-list-file> <image-save-path>'

def get_all_images():
    print USAGE
    if len(sys.argv) != 3:
        return
    path = sys.argv[2]
    images = open(sys.argv[1]).readlines()
    count = 0
    cur_count = 0
    cur_name = ''
    for image in images:
        desc = image.split('\t')
        if len(desc) != 5:
            print 'error format of ' + desc
            continue
        url = desc[1]
        sub = url.split('/')[-1]
        point_pos = sub.find('.')
        if point_pos == -1:
            sub = '.jpg'
        else:
            sub = sub[point_pos:]
        if cur_name != desc[0]:
            cur_name = desc[0]
            cur_count = 0
        file_name = path + '/' + cur_name + '-' + str(cur_count) + sub;
        print '>> no.' + str(count) + '.\t Downloading [' + url + '] to [' + file_name + ']...'
        try:
            os.system('wget %s -O %s -T 5 -t 3' %(url, file_name))
            # urllib.urlretrieve(url, file_name)
        except Exception, e:
            print '  | unknown error of ' + url + ' --> ' + file_name + ': ', e
        cur_count += 1
        count += 1

get_all_images()
