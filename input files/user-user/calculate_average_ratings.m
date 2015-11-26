clear all
clc
close all
a = csvread('train.csv',1,0);
a = sortrows(a);
sizes = max(a);
user_rating_mat = zeros(sizes(1),sizes(2));
for i = 1:size(a,1)
   user_rating_mat(a(i,1),a(i,2)) = a(i,3);
end

avg_rat = zeros(sizes(1),1);
flag = 1;
begin = 1;
endd = 1;
for i = 1:size(a,1)
    if a(i,1) ~= flag
       
       endd = i - 1;
       rows = a(begin:endd,3);
       if begin <= endd
           avg_rat(flag) = mean(rows);
       end
       begin = i;
       flag = a(i,1);
    end
end
csvwrite('avg_rat.csv',avg_rat);