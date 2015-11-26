train_data = csvread('train.csv',1,0);
sizes = max(train_data);
user_rating_mat = zeros(sizes(2),sizes(1));
for i = 1:size(train_data,1)
   user_rating_mat(train_data(i,2),train_data(i,1)) = train_data(i,3);
end
%correlation = corr(user_rating_mat, user_rating_mat,'type','Pearson');
avg_rat = csvread('avg_rat.csv');
similarity = zeros(sizes(1),sizes(1));
flag = 1;
begin = 1;
endd = 1;
for i = 1:size(train_data,1)
    if train_data(i,2) ~= flag
       flag = train_data(i,2);
       endd = i - 1;
       rows = train_data(begin:endd,:);
       if begin < endd
           for j = 1:size(rows)
              for k = (j+1):size(rows)
                similarity(rows(j,1),rows(k,1)) = similarity(rows(j,1),rows(k,1)) + (rows(j,3) - avg_rat(rows(j,1))) * (rows(k,3) - avg_rat(rows(k,1)));
              end
           end
       end
       begin = i;
    end
end

for i = 1:sizes(1)
    for j= (i+1):sizes(1)
        if similarity(i,j) ~= 0
            similarity(j,i) = similarity(i,j);
        end
            
    end
end

numberofsimilarusers=10;
similar_users = zeros(size(train_data,1), numberofsimilarusers*2);
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

csvwrite('similarity_pearson.csv', similar_users);
