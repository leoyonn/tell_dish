% make the weka file from image_root.
% 1. new weka file, set attributes;
% 2. add weka data for each image in image_root
function make_weka(image_root, weka_file_name)
    global DISHES DISH_NAME_SPLITTER;
    DISH_NAME_SPLITTER = ' ';
    file_list = dir([image_root, '*.*']);
    % find all dishes
    dish_set = java.util.HashSet;
    for i = 1:length(file_list)
        file_name = file_list(i).name;
        if ~is_image(file_name)
            disp(['not an image file: ', file_name]);
            continue;
        end
        dish = dish_name(file_name);
        if dish
            dish_set.add(dish);
        end
    end
    DISHES = char(dish_set);
    DISHES = strrep(DISHES(2:(length(DISHES) - 1)), ' ', '');
    disp(DISHES);
    DISHES = char(DISHES);
    weka_file = new_weka_file(weka_file_name);
    for i = 1:length(file_list)
        file_name = file_list(i).name;
        if ~is_image(file_name)
            disp(['not an image file: ', file_name]);
            continue;
        end
        image = imread([image_root, file_name]);
        % axis off;
        % subplot(10, ceil(length(file_list) / 10), i);
        % imshow(image);
        disp(['processing ', file_name, '...']);
        dish = dish_name(file_name);
        add_weka_data(weka_file, hsvhist(image), rgbhist(image), dish);
    end
    close_weka_file(weka_file);
end

% should call close_weka_file after using!
function weka_file = new_weka_file(weka_file_name)
    global DIM_HSV DIM_RGB;
    DIM_HSV = 36;
    DIM_RGB = 78;
    weka_file = fopen(weka_file_name, 'w');
    fprintf(weka_file, '@RELATION image_features\n\n');
    for i=1:DIM_HSV
        fprintf(weka_file, '@ATTRIBUTE %s_%d NUMERIC\n', 'hsvhist', i);
    end
    for i=1:DIM_RGB
        fprintf(weka_file, '@ATTRIBUTE %s_%d NUMERIC\n', 'rgbhist', i);
    end
    global DISHES;
    fprintf(weka_file, '@ATTRIBUTE label {%s}\n', DISHES);
    fprintf(weka_file, '\n@DATA\n\n');
end

% close the file 
function close_weka_file(weka_file)
    fclose(weka_file);
end

% add 1 data row
function add_weka_data(weka_file, feature_hsvhist, feature_rgbhist, dish)
    global DIM_HSV DIM_RGB;
    if length(feature_hsvhist) ~= DIM_HSV ...
        || length(feature_rgbhist) ~= DIM_RGB
        disp('wrong dim of feature!');
        return
    end
    fprintf(weka_file, '%.4f,', feature_hsvhist);
    fprintf(weka_file, '%.4f,', feature_rgbhist);
    fprintf(weka_file, '%s\n', dish);
end

% get dish name from file name. using ' ' to split. depends.
function dish = dish_name(file_name)
    global DISH_NAME_SPLITTER;
    dish = file_name(1:(strfind(file_name, DISH_NAME_SPLITTER) - 1));
end