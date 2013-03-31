#!/bin/bash
####################################################################
## 灏嗗綋鍓嶆枃浠跺す涓璼vn鏈湴鏀瑰姩(濡侫/A+/M/D)浼犺緭鍒癶ost鎸囧畾鐨勪綅缃�
## 浼氭敼鍔細
## 1. 鍘婚櫎svn st 涓瑼+鐨�鍙凤紱
## 2. 灏�A/A+/M/R 鏂囦欢浼犺緭鍒癶ost锛屽鏋淎涓虹洰褰曪紝鍒欎細鍒涘缓鐩綍锛�## 3. 灏咲鏍囪瘑鐨勬枃浠跺湪host涓婂垹闄わ紱
##
## @author liuyang
####################################################################

host="localhost"
dir="/disk1/liuyang/sanji/"

svn up
svn st > tmp
awk -v host=$host -v dir=$dir '
function isDir(file) {
    r = system(" if [ -d "file" ]; then return 1; else return 0; fi");
    return r;
}

function A(file) {
    if (isDir(file)) {
        cmd = "ssh "host" \"mkdir "dir""file"\"";
        print cmd;
        system(cmd);
    } else {
        cmd = "scp "file" "host":"dir""file;
        print cmd;
        system(cmd);
    }
}

function Aplus(file) {
    printf("remove + of "file": ");
    system("svn revert "file);
    system("svn add "file);
    A(file);
}

function M(file) {
    if (isDir(file)) {
        print "Ignore dir modification: "file;
        return;
    }
    file = $2;
    cmd = "scp "file" "host":"dir""file;
    print cmd;
    system(cmd);
}

function D(file) {
    file = $2;
    cmd = "ssh "host" \"rm -rf "dir""file"\"";
    print cmd;
    system(cmd);
}

{
    printf($0": \n\t");

    ## 1. 鍒犻櫎A+涓殑+锛屽苟scp
    if(NF == 3 && $2 == "+") {
        Aplus($3);
    }

    ## 2. 鍒涘缓A鐨勭洰褰�    else if($1 == "A") {
        A($2);
    }

    ## 3. 灏咥/M/R鐨勬枃浠�scp涓婂幓
    else if($1 == "M" || $1 == "R") {
        M($2);
    }

    ## 4. 灏咲鐨勬枃浠舵垨鏂囦欢澶瑰湪host涓婂垹闄�    else if($1 == "D") {
        D($2);
    } 

    ## 5. 鍏跺畠
    else {
        print "svn status ignored: "$0;
    }
}' tmp
rm tmp
echo done!
