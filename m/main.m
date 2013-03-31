%% list files and get each file's image features
% @author leo
function main(image_root)
    if (image_root(end) ~= '/')
        image_root = [image_root,  '/'];
    end
    % make_weka(image_root, weka_file_name);
    nc = 100;
    gen_raw_surf(image_root, '../surf_raw.dat', '../surf_count.dat');
    cluster_features(nc, '../surf_raw.dat', '../surf_pt_cls.dat', '../surf_c.dat');
    after_cluster(nc, '../surf_pt_cls.dat', '../surf_count.dat', '../surf.dat');
    disp('all done!');
