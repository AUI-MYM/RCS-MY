a = csvread('train.csv',1,0);
test_data = csvread('test.csv',1,0);
sizes = max(a);
flag = 1;
begin = 1;
endd = 1;
means = [];
for i = 1:size(a,1)
    if a(i,2) ~= flag
       flag = a(i,2);
       endd = i - 1;
       rows = a(begin:endd,:);
       if begin + 50 < endd
           means = [means; rows(1,2) mean(rows(:,3))];
       end
       begin = i;
    end
end
means = sortrows(means,-2);
csvwrite('top_movies2.csv',means);