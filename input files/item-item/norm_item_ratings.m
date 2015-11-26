data = csvread('train.csv', 1,0);
s = size(data,1);
m = max(data);
norms = zeros(m(2),1);
flag = 1;
sum = 0;
for i=1:s
    if flag ~= data(i,2)
        norms(flag) = sqrt(sum);
        sum = data(i,3)*data(i,3);
        flag = data(i,2);
    else
        sum = sum + data(i,3)*data(i,3);
    end
end
csvwrite('norm_item_ratings.csv', norms);