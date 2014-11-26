
load("lengthCounts.txt" )
# lengthCounts.txt contains
#  lengthCounts - matrix of col 'word length' and col 'count of words of that length'

lens = lengthCounts(:,1)
cts = lengthCounts(:,2)

totalSize = sum( lens .* cts)

