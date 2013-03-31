% if the file ends with '.jpg' etc,...
function is = is_image(name)
    l = length(name);
    if l < 3
        is = false;
        return
    end
    sub = lower(name(l - 3: l));
    if strcmp(sub, '.jpg') || strcmp(sub, 'jpeg') || strcmp(sub, '.gif') ...
        || strcmp(sub, '.png')  || strcmp(sub, '.bmp') 
        is = true;
    else
        is = false;
    end