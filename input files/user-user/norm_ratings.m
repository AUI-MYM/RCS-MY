clear all
clc
close all

a = csvread('train.csv',1,0);
%{test_data = csvread('test.csv',1,0);}%
sizes = max(a);
a_sorted = sortrows(a);
user_rating_mat = zeros(sizes(1),sizes(2));
for i = 1:size(a,1)
   user_rating_mat(a(i,1),a(i,2)) = a(i,3);
end

norms = sqrt(sum(user_rating_mat.^2,2));

for j = 1:size(a,1)
    if norms(a(j,1)) ~= 0
        a(j,3) = a(j,3) / norms(a(j,1));
    end
end

csvwrite('norm_ratings_sorted.csv', sortrows(a));