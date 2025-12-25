package ggm.board.domain.member.dto;

import ggm.board.domain.auth.entity.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {
    private long id;
    private String name;
    private String email;
    private LocalDateTime joinDate;
    private LocalDateTime birthDate;
    private UserRole role;
    private String type;;
}
