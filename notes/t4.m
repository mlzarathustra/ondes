y=[];
x=-15:.1:15
for xn=x
  y=[y, abs(sin(xn)/xn)];
endfor

plot(x,y)