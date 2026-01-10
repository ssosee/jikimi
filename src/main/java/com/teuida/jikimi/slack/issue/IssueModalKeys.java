package com.teuida.jikimi.slack.issue;

abstract public class IssueModalKeys {
    // Command
    public static final String COMMAND_ISSUE = "/issue";

    // Callback IDs
    public static final String ISSUE_MODAL = "issue_modal";
    public static final String ISSUE_DELETE_CONFIRM_MODAL = "issue_delete_confirm_modal";

    // Block IDs
    public static final String BLOCK_ENVIRONMENT = "block_environment";
    public static final String BLOCK_COURSE_TYPE = "block_course_type";
    public static final String BLOCK_APPLICATION_TYPE = "block_application_type";
    public static final String BLOCK_TITLE = "block_title";
    public static final String BLOCK_DESCRIPTION = "block_description";
    public static final String BLOCK_USERGROUP = "block_usergroup";
    public static final String BLOCK_USER_EMAIL = "block_user_email";
    // Action IDs
    public static final String ACTION_ENVIRONMENT = "action_environment";
    public static final String ACTION_COURSE_TYPE = "action_course_type";
    public static final String ACTION_APPLICATION_TYPE = "action_application_type";
    public static final String ACTION_TITLE = "action_title";
    public static final String ACTION_DESCRIPTION = "action_description";
    public static final String ACTION_USERGROUP = "action_usergroup";
    public static final String ACTION_USER_EMAIL = "action_user_email";
    public static final String ACTION_MORE_OPTIONS = "action_more_options";
    // ===== Issue Block Action IDs =====
    public static final String ACTION_CREATE_TICKET = "action_create_ticket";
    public static final String ACTION_SOLVE = "action_solve";
    public static final String ACTION_ASSIGN_TO_ME = "action_assign_to_me";
    public static final String ACTION_SELECT_ASSIGNEE = "select_assignee";

    // ===== Button Values =====
    public static final String VALUE_CREATE_TICKET = "value_create_ticket";
    public static final String VALUE_SOLVE = "value_solve";
    public static final String VALUE_ASSIGN_TO_ME = "value_assign_to_me";
    public static final String VALUE_DELETE_ISSUE = "value_delete_issue";
}
