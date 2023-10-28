package ru.findFood.auth.services.specifications;


import org.springframework.data.jpa.domain.Specification;
import ru.findFood.auth.entities.AuthUser;


public class AuthUserSpecifications {
    public static Specification<AuthUser> getByRolesTitleEqualsTo(String roleTitle){
        return (root, criteriaQuery, criteriaBuilder) -> {
            if(roleTitle.equals("NUTRITIONIST")) {
                criteriaQuery.where(
                        criteriaBuilder.equal(criteriaBuilder.size(root.get("roles")), 2))
                    .groupBy(root.get("id"), root.get("roles").get("title"))
                    .having(
                        criteriaBuilder.equal(root.get("roles").get("title"), roleTitle));
            } else if(roleTitle.equals("ADMIN")) {
                criteriaQuery.where(
                        criteriaBuilder.equal(root.get("roles").get("title"), roleTitle));
            }
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<AuthUser> getByRolesWhereSizeGreaterThan(Integer size){
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true).where(
                    criteriaBuilder.greaterThan(
                            criteriaBuilder.size(root.get("roles")), size));
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<AuthUser> emailLike(String partEmail){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("email"), String.format("%%%s%%", partEmail));
    }

}
