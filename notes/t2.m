

for fc=[55, 110, 220, 440, 880, 1000, 1600, 3200, 4000, 6400, 8000, 10000, 15000, 20000]

  [b,a] = butter(4, fc/(44100/2));
  printf("a[%d]='''\n",fc)
  printf("%2.15f\n", a')
  printf("'''\n\n")

  printf("b[%d]='''\n",fc)
  printf("%2.15f\n", b')
  printf("'''\n\n")

end
