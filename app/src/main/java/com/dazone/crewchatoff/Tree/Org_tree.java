package com.dazone.crewchatoff.Tree;

import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;

import java.util.*;

public class Org_tree {
    public static TreeUserDTO buildTree(List<TreeUserDTO> dtos) throws Exception {
        TreeUserDTO root = new TreeUserDTO(0, 0, "Dazone");
        HashMap<Integer, ArrayList<TreeUserDTO>> jsonDataMap = new HashMap<>();
        for (TreeUserDTO dto : dtos) {
            if(dto.getStatus()==0)
            if (jsonDataMap.containsKey(dto.getParent())) {
                if (dto.getType() == 2) {
                    ArrayList<TreeUserDTO> currentList = jsonDataMap.get(dto.getParent());
                    currentList.add(dto);
                    Collections.sort(currentList, (r1, r2) -> {
                        if (r1.getPositionSortNo() > r2.getPositionSortNo()) {
                            return 1;
                        }else if (r1.getPositionSortNo() == r2.getPositionSortNo()) {
                            return 0;
                        } else {
                            return -1;
                        }
                    });
                } else {
                    jsonDataMap.get(dto.getParent()).add(dto);
                }
            } else {
                ArrayList<TreeUserDTO> subordinates = new ArrayList<>();
                subordinates.add(dto);
                jsonDataMap.put(dto.getParent(), subordinates);
            }
        }

        for (TreeUserDTO subordinate : jsonDataMap.get(root.getId())) {
            root.addSubordinate(subordinate);
            subordinate.setParent(root.getId());
            // Sort by name
            buildSubTree(subordinate, jsonDataMap);
        }
        return root;
    }

    private static void buildSubTree(TreeUserDTO parent, HashMap<Integer, ArrayList<TreeUserDTO>> jsonDataMap) {
        List<TreeUserDTO> subordinates = jsonDataMap.get(parent.getId());
        if (subordinates != null) {
            for (TreeUserDTO subordinate : subordinates) {
                subordinate.setParent(parent.getParent());

                parent.addSubordinate(subordinate);
                if (subordinate.getType() == 0) {
                    buildSubTree(subordinate, jsonDataMap);
                }
            }
        }
    }

}
