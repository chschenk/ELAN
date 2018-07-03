/*
 * Project:	MMFPlayer, Microsoft Media Foundation Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Nov. 2011
 */
#include "MMFPlayer.h"
#include "MMFPlayerMap.h"

void MMFPlayerMap::put(long id, MMFPlayer* player) {
	plMap.insert(map<long, MMFPlayer *>::value_type(id, player));
	//plMap.insert(playerMapType::value_type(id, player));
}

MMFPlayer* MMFPlayerMap::get(long id) {
	map<long, MMFPlayer *>::iterator iter = plMap.find(id);
	//playerMapType::iterator iter = plMap.find(id);
	if (iter != plMap.end()) {
		return iter->second;
	}
	return NULL;
}

MMFPlayer* MMFPlayerMap::remove(long id) {
	map<long, MMFPlayer *>::iterator iter = plMap.find(id);
	//playerMapType::iterator iter = plMap.find(id);
	if (iter != plMap.end()) {
		MMFPlayer* player = iter->second;
		plMap.erase(iter);
		return player;
	}
	return NULL;
}

bool MMFPlayerMap::containsKey(long id) {
	playerMapType::iterator iter = plMap.find(id);
	if (iter == plMap.end()) {
		return false;
	}
	return true;
}