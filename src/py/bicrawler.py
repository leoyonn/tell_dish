# -*- coding: utf-8 -*-
#!/usr/bin/python
# author: leo
# data: 2012-09-25

import urllib, urllib2, sys, os, json, time, redis

FAKE_UA = 'Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.218 Safari/535.1'
REQ_PARAMS = {'Referer': 'http://baidu.com', 'user-agent': FAKE_UA}
BAIDU_URL = "http://image.baidu.com/i?tn=baiduimagejson"\
     + "&ct=201326592&cl=2&lm=-1&st=-1&fm=&fr=&sf=1&fmq=1352199039094_R"\
     + "&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2"\
     + "&ie=utf-8&oe=utf-8&rn=60&171501879773.19495&1287771110186.968"\
     + "&word=%s&pn=%d" # %(query, start)
def main():
#    url = BAIDU_URL % (u"麻辣香锅", 50)
#    response = urllib2.urlopen(urllib2.Request(url, None, REQ_PARAMS)).read()
#    print response
    r = redis.Redis(host='localhost', port=20890, db=0)
    r.set('key', 'value')
    print r.get('key')
main()
