#include "DSPlayer.h"
#include <map>
using namespace std;

#ifndef included_PlayerMap
#define included_PlayerMap

typedef map<long, DSPlayer*> playerMapType;

class PlayerMap {
public: 
	void put(long, DSPlayer*);
	DSPlayer* get(long);
	DSPlayer* remove(long);
	bool containsKey(long);
private:
	//static map<long, DSPlayer *> plMap;
	playerMapType plMap;
};

#endif