function out = rgbhist(I)
%RGBHIST   Histogram of RGB values.
siz=size(I);
I1=reshape(I,siz(1)*siz(2),siz(3));  % 每个颜色通道变为一列
I1=double(I1);
[N,X]=hist(I1, 0:10:255);    % 如果需要小矩形宽一点，划分区域少点，可以把步长改大，比如0:5:255
hist_count = 1./sum(N);
N = N * hist_count(1);
% bar(X, N(:,[3 2 1]));    % 柱形图，用N(:,[3 2 1])是因为默认绘图的时候采用的颜色顺序为b,g,r,c,m,y,k，跟图片的rgb顺序正好相反，所以把图片列的顺序倒过来，让图片颜色通道跟绘制时的颜色一致
% xlim([0 255])
% hold on
% plot(X, N(:, [3 2 1]));    % 上边界轮廓
% hold off
siz = size(N);
out = reshape(N, siz(1) * siz(2), 1);