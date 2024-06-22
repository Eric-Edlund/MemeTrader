package org.example;

/**
 * Represents a comment on an article.
 */
//@Entity
//@Table(name = "Comment")
//public class Comment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "userId", nullable = false)
//    private Account user;
//
//    @ManyToOne
//    @JoinColumn(name = "articleId", nullable = false)
//    private Article article;
//
//    @Column(name = "commentText", nullable = false)
//    private String commentText;
//
//    @Column(name = "postDate", nullable = false)
//    private Date postDate;
//
//    // getters and setters
//}
//
//@Entity
//public class Comment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "userId", nullable = false)
//    private Account user;
//
//    @ManyToOne
//    @JoinColumn(name = "articleId", nullable = false)
//    private Article article;
//
//    @Column(nullable = false)
//    private String commentText;
//
//    @Column(nullable = false, updatable = false, insertable = false)
//    @CreationTimestamp
//    private Date postDate;
//
//    // Getters and Setters
//}