
freqs=[];
%for n=0:9; freqs=[freqs, 27.5 * 2^n]; end

%for n=1:1000; freqs=[freqs, 20*n]; end
for n=1:100; freqs=[freqs, 200*n]; end

% freqs'

as=[];
bs=[];

for f=freqs; 
  [b,a]=butter(4,f/(44100/2));
  as=[as,a'];
  bs=[bs,b'];
  %printf("%f ",n); 
  
end

freqs;
as 
bs

figure(1);
hold on;
for n=1:10; plot(as(:,n)); end
title("A values")

figure(2);
hold on;
for n=1:10; plot(bs(:,n)); end
title("B values")

% for sweep and Q, a "lattice filter" is the thing to use

% NOTE - the 'latcfilt' function belongs to the signal package 
% from Octave Forge but has not yet been implemented.



