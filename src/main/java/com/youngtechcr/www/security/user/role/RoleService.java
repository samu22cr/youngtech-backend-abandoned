package com.youngtechcr.www.security.user.role;

import com.youngtechcr.www.exceptions.HttpErrorMessages;
import com.youngtechcr.www.exceptions.custom.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public boolean doThisRolesExist(List<Role> roleList) {
        return roleList
                .stream()
                .allMatch(role -> this.roleRepository.existsById(role.getId()));
    }

    @Transactional(readOnly = true)
    public Role findRoleById(Integer roleId) {
        return this
                .roleRepository
                .findById(roleId)
                .orElseThrow(
                        () ->
                        new NoDataFoundException(
                                HttpErrorMessages.NO_ELEMENT_WITH_THE_REQUESTED_ID_WAS_FOUND
                        )
                );
    }


    @Transactional(readOnly = true)
    public Set<Role> mapOptionsToRoles(RoleOption... roleOptions) {
        Set<Role> existingRoles = new HashSet<>();
        for (int i = 0; i < roleOptions.length; i++) {
            var currentOption = roleOptions[i];
            Role toBeAdded = findRoleByName(currentOption.getIdentifier());
            existingRoles.add(toBeAdded);
        }
        return existingRoles;
    }


    @Transactional(readOnly = true)
    public Role findRoleByName(String name) {
        return this
                .roleRepository
                .findByName(name)
                .orElseThrow(
                        () ->  new NoDataFoundException(
                                HttpErrorMessages.
                                        NO_ELEMENT_WITH_THE_REQUESTED_NAME_WAS_FOUND
                        ) );
    }

}
