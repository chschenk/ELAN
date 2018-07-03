/*
 * Project:	JDSPlayer, Direct Show Media Player for Java
 * Author:	Han Sloetjes
 * Version:	1.0, Oct. 2010
 */
#include "PlayerMap.h"
#include "DSPlayer.h"


void PlayerMap::put(long id, DSPlayer* player) {
	plMap.insert(map<long, DSPlayer *>::value_type(id, player));
	//plMap.insert(playerMapType::value_type(id, player));
}

DSPlayer* PlayerMap::get(long id) {
	map<long, DSPlayer *>::iterator iter = plMap.find(id);
	//playerMapType::iterator iter = plMap.find(id);
	if (iter != plMap.end()) {
		return iter->second;
	}
	return NULL;
}

DSPlayer* PlayerMap::remove(long id) {
	map<long, DSPlayer *>::iterator iter = plMap.find(id);
	//playerMapType::iterator iter = plMap.find(id);
	if (iter != plMap.end()) {
		DSPlayer* player = iter->second;
		plMap.erase(iter);
		return player;
	}
	return NULL;
}

bool PlayerMap::containsKey(long id) {
	playerMapType::iterator iter = plMap.find(id);
	if (iter == plMap.end()) {
		return false;
	}
	return true;
}