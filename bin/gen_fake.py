import random

for l in open('score.txt').readlines():
	sum = 0
	for i in range(0,9):
		sum = sum + float(l.split()[0][i])	
#	sum = 1- sum/10
#	sum = sum/100 + 0.9;
	print l.split()[0] + ' ' + str(sum)
	#print l.split()[0] + ' ' + str(random.randint(0,9))
	#print '1' + l.strip('\n')

#for l in open('score.txt').readlines():
#	print l.strip('\n')[0:14]
#for l in open('score.txt').readlines():
#	print '0' + l.strip('\n')

