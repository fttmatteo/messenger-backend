package app.domain.model.auth;

/**
 * Respuesta de login con cookies HttpOnly.
 * NO incluye los tokens (se envían en cookies).
 */
public class LoginResponse {

    private String role;
    private String message;
    private UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(String role, String message, UserInfo user) {
        this.role = role;
        this.message = message;
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private Long id;
        private String name;
        private Long document;
        private String dealershipName;

        public UserInfo() {
        }

        public UserInfo(Long id, String name, Long document, String dealershipName) {
            this.id = id;
            this.name = name;
            this.document = document;
            this.dealershipName = dealershipName;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getDocument() {
            return document;
        }

        public void setDocument(Long document) {
            this.document = document;
        }

        public String getDealershipName() {
            return dealershipName;
        }

        public void setDealershipName(String dealershipName) {
            this.dealershipName = dealershipName;
        }
    }
}
