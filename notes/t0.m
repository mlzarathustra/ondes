rs=[]
for d=1:200
  a=1;
  k=1/d;
  m=1/d;

  while a(length(a)) > 0
    a(length(a) + 1) = a(length(a)) - (k+a(length(a)) * m);
  endwhile
  rs(length(rs)+1)=length(a);
endfor

plot(rs);

