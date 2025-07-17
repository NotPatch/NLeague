package com.notpatch.nLeague.model;

import com.notpatch.nLeague.util.ColorUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class League {

    private final String id;
    private final String displayName;
    private final int requiredPoints;
    private final List<String> promotionCommands;

    public String getDisplayName() {
        return ColorUtil.hexColor(displayName);
    }
}
