package com.wowraid.jobspoon.user_term.entity;

import com.wowraid.jobspoon.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user_wordbook_folder",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_folder_owner_normalized",
                columnNames = {"account_id", "normalized_folder_name"}
        )
)
public class UserWordbookFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "FK_swf_account_cascade"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @Setter
    @Column(name = "folder_name", nullable = false, length = 50)
    private String folderName;

    @Column(name = "normalized_folder_name", nullable = false, length = 50)
    private String normalizedFolderName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    @PrePersist
    protected void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.sortOrder == null) this.sortOrder = 0;
        ensureNormalized();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        ensureNormalized();
    }

    private void ensureNormalized() {
        if (this.folderName != null) {
            this.normalizedFolderName = normalize(this.folderName);
        }
    }

    private static String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public UserWordbookFolder(Account account, String folderName, Integer sortOrder) {
        this.account = account;
        this.folderName = folderName;
        this.sortOrder = sortOrder;
    }

    public UserWordbookFolder(Account account, String folderName, Integer sortOrder, String normalized) {
        this.account = account;
        this.folderName = folderName;
        this.normalizedFolderName = normalized;
        this.sortOrder = sortOrder;
    }
}
