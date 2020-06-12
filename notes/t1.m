
%  simulation of envelope 
sampleRate = 44100
rate = 2000 % milliseconds
level = 0
curLevel = 100

% log(100) is 4.605170185988092
div = 4.61499
d = (sampleRate * rate / 1000.0) / div;
k = m = 1.0/d;


function stepResult = nextVal(curLevel, sampleRate, rate, level, d, k, m) 
    if curLevel == level
        stepResult.level = curLevel;
        stepResult.done = true;
        return;
    endif

    delta = level - curLevel;
    nextLevel = curLevel + ( sign(delta) * k  +  delta * m);

    stepResult.done = false;
    if  ((curLevel > level && nextLevel <= level) || (curLevel < level && nextLevel >= level))
        stepResult.done = true;
        stepResult.level = level;
        return;
    endif
    
    %  clip between 0.0 and 100.0
    stepResult.level = max(0.0, min(100.0, nextLevel));
    return;
endfunction


rs = [];
for i=1:100000 
  rs(length(rs)+1) = curLevel;
  stepResult = nextVal(curLevel, sampleRate, rate, level, d, k, m);
  % printf("stepResult.level: %f\n", stepResult.level);
  if (stepResult.done) 
    printf("done");
    break;
  endif
  curLevel = stepResult.level;
endfor
  
printf("length(rs) is %d\n",length(rs))  
  
    
