package com.dazone.crewchatoff.TestMultiLevelListview;

import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;

public interface NLevelListItem {
	TreeUserDTO getObject();
	boolean isExpanded();
	void toggle();
	NLevelListItem getParent();
	int getLevel();
}
