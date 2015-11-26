clear all
clc
close all
a = csvread('train.csv',1,0);
test_data = csvread('test.csv',1,0);
sizes = max(a);
%{
flag = 1;
begin = 1;
endd = 1;
means = [];
for i = 1:size(a,1)
    if a(i,2) ~= flag
       flag = a(i,2);
       endd = i - 1;
       rows = a(begin:endd,:);
       if begin + 20 < endd
           means = [means; rows(1,2) mean(rows(:,3))];
       end
       begin = i;
    end
end
means = sortrows(means,-2);
csvwrite('top_movies.csv',means);
%}
%a_sorted = sortrows(a);
%a(:,3) = a(:,3) - mean(a(:,3));
%a_sorted = sortrows(a);
%csv_write('norm_ratings2.csv',a_sorted);
user_rating_mat = zeros(sizes(1),sizes(2));
for i = 1:size(a,1)
   user_rating_mat(a(i,1),a(i,2)) = a(i,3);
end
norms = sqrt(sum(user_rating_mat.^2,2));
similarity = zeros(sizes(1),sizes(1));
flag = 1;
begin = 1;
endd = 1;
for i = 1:size(a,1)
    if a(i,2) ~= flag
       flag = a(i,2);
       endd = i - 1;
       rows = a(begin:endd,:);
       if begin < endd
           for j = 1:size(rows)
              for k = (j+1):size(rows)
                similarity(rows(j,1),rows(k,1)) = similarity(rows(j,1),rows(k,1)) + rows(j,3)*rows(k,3);
              end
           end
       end
       begin = i;
    end
end

for i = 1:sizes(1)
    for j= (i+1):sizes(1)
        if similarity(i,j) ~= 0
            similarity(i,j) = similarity(i,j)/ (norms(i)*norms(j));
            similarity(j,i) = similarity(i,j);
        end
            
    end
end

similar_users = zeros(size(a,1),20);
numberofsimilarusers=10;
for i = 1:sizes(1)
   [list, index] = sort(similarity(i,:), 'descend');
   sizeTemp = size(list(list~=0),2);
   if sizeTemp > numberofsimilarusers
        similar_users(i,1:numberofsimilarusers) = index(1:numberofsimilarusers);
        similar_users(i,(numberofsimilarusers+1):(numberofsimilarusers*2)) = list(1:numberofsimilarusers);
   else
        similar_users(i,1:numberofsimilarusers) = [index(1:sizeTemp) zeros(1,numberofsimilarusers-sizeTemp)];
        similar_users(i,(numberofsimilarusers+1):(numberofsimilarusers*2)) = [list(1:sizeTemp) zeros(1,numberofsimilarusers-sizeTemp)];
   end
end
csvwrite('similarity.csv',similar_users);
