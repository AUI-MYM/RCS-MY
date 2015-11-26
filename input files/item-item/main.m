icm = csvread('icm.csv',1,0);
sizes = max(icm);
item_feature_mat = zeros(sizes);
for i=1:size(icm,1)
   item_feature_mat(icm(i,1),icm(i,2)) = 1;
end

norms = sqrt(sum(item_feature_mat.^2,2));
csvwrite('norms.csv',norms);
%{
similarity = single(zeros(sizes(1)));
flag = 1;
begin = 1;
endd = 1;
for i = 1:size(icm,1)
    if icm(i,2) ~= flag
       flag = icm(i,2);
       endd = i - 1;
       rows = icm(begin:endd,:);
       if begin < endd
           for j = 1:size(rows)
              for k = (j+1):size(rows)
                similarity(rows(j,1),rows(k,1)) = similarity(rows(j,1),rows(k,1)) + 1;
              end
           end
       end
       begin = i;
    end
end
%}