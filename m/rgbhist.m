function out = rgbhist(I)
%RGBHIST   Histogram of RGB values.
siz=size(I);
I1=reshape(I,siz(1)*siz(2),siz(3));  % ÿ����ɫͨ����Ϊһ��
I1=double(I1);
[N,X]=hist(I1, 0:10:255);    % �����ҪС���ο�һ�㣬���������ٵ㣬���԰Ѳ����Ĵ󣬱���0:5:255
hist_count = 1./sum(N);
N = N * hist_count(1);
% bar(X, N(:,[3 2 1]));    % ����ͼ����N(:,[3 2 1])����ΪĬ�ϻ�ͼ��ʱ����õ���ɫ˳��Ϊb,g,r,c,m,y,k����ͼƬ��rgb˳�������෴�����԰�ͼƬ�е�˳�򵹹�������ͼƬ��ɫͨ��������ʱ����ɫһ��
% xlim([0 255])
% hold on
% plot(X, N(:, [3 2 1]));    % �ϱ߽�����
% hold off
siz = size(N);
out = reshape(N, siz(1) * siz(2), 1);