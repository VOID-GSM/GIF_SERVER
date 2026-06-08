package com.example.gifserverv2.domain.project.dto.request;

import java.util.List;

public class UpdateProjectRequest {
    private String name;
    private String teamName;
    private String description;
    private List<Long> addMemberIds;
    private List<Long> removeMemberIds;

    public String getName() { return name; }
    public String getTeamName() { return teamName; }
    public String getDescription() { return description; }
    public List<Long> getAddMemberIds() { return addMemberIds; }
    public List<Long> getRemoveMemberIds() { return removeMemberIds; }
}