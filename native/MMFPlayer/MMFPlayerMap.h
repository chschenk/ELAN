#include "MMFPlayer.h"
#include <map>
using namespace std;

#ifndef included_MMFPlayerMap
#define included_MMFPlayerMap

typedef map<long, MMFPlayer*> playerMapType;

class MMFPlayerMap {
public: 
	void put(long, MMFPlayer*);
	MMFPlayer* get(long);
	MMFPlayer* remove(long);
	bool containsKey(long);
private:
	//static map<long, MMFPlayer *> plMap;
	playerMapType plMap;
};

#endif