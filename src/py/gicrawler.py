# -*- coding: utf-8 -*-
#!/usr/bin/python
# author: leo
# data: 2012-09-25

import urllib, urllib2, sys, os, json, time

# base url, rsz should less than 8.
RSZ = 8
BASE_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=" + str(RSZ)\
            + '&imgsz=small|medium|large|xlarge|xxlarge|huge'
# https://developers.google.com/custom-search/v1/using_rest

IMG_URLS_FILE = None
IMG_PATH = "."

# parse image list from google-image
def parse_image_list(query, results):
    if results is None or not results.has_key('responseData'):
        return 0
    results = results['responseData']
    if results is None or not results.has_key('results'):
        return 0
    count = 0
    results = results['results']
    for r in results:
        img = '\t'.join([query.decode('utf8'), r['url'], r['width'] + 'x' + r['height'],
                     r['titleNoFormatting'], r['contentNoFormatting']])
        IMG_URLS_FILE.write(img.encode("utf8") + '\n')
        print '  | got ' + img.encode("utf8")
        count += 1
    IMG_URLS_FILE.flush()
    return count

# get query from google. num round up to RSZ's multiple
def get_image_list(query, num):
    print '>> getting [' + query + '](count: ' + str(num) + ')...',
    start = 0
    while start < num:
        try:
            url = BASE_URL + '&q=' + urllib.quote(query) + "&start=" + str(start)
            request = urllib2.Request(url, None, {'Referer': 'http://leoyonn.com'})
            response = urllib2.urlopen(request)
            results = json.load(response)
            if parse_image_list(query, results) < RSZ:
                print 'no more results for [' + query + ']...'
                break;
            start = start + RSZ
        except :
            print sys.exc_info()
            return 0
    print '--| all got [' + str(start) + ']...'
    time.sleep(1)
    return start

USAGE = '>> usage: gicrawler <dish-list-file> <count-to-get> <image-save-path>'

# get dishes's url of dish-list, if some dish failed, try again
def main():
    print USAGE
    if len(sys.argv) != 4:
        return
    dish_list_file = sys.argv[1]
    count = int(sys.argv[2])
    global IMG_URLS_FILE
    IMG_URLS_FILE = open(sys.argv[3] + "/image_urls.list", 'w')
    if not os.path.isfile(dish_list_file):
        print 'file not exists... ' + USAGE
        return
    dish_list = open(dish_list_file).readlines()
    missed_dish_list = []
    # try 10 round to get all missed dishes
    for round in range(10):
        if len(dish_list) == 0:
            break
        print '===========@@ round.%d: all [%d] dishes left.@@===========' %(round, len(dish_list))
        for dish in dish_list:
            dish = dish.strip('\n')
            if get_image_list(dish, count) == 0:
                missed_dish_list.append(dish)
        # after each round, use missed dishes as input
        dish_list = missed_dish_list

main()
