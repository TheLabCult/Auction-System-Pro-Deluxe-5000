bash command to get folder tree:
```tree -I "node_modules|.git|build|dist|target|*.log|apache-maven-3.9.15|resources|sqlite|*.docx|*.pdf"```


├── README.md
├── Tree.md
├── auction-client
│   ├── dependency-reduced-pom.xml
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── auction
│                       └── client
│                           ├── ClientLauncher.java
│                           ├── ClientMain.java
│                           ├── controller
│                           │   ├── AdminController.java
│                           │   ├── AuctionDetailController.java
│                           │   ├── AuctionListController.java
│                           │   ├── ItemDetailDialogController.java
│                           │   ├── LoginController.java
│                           │   ├── RegisterController.java
│                           │   ├── SellerDashboardController.java
│                           │   └── UserProfileController.java
│                           ├── network
│                           │   └── ServerConnection.java
│                           ├── session
│                           │   └── ClientSession.java
│                           └── util
│                               ├── AlertUtil.java
│                               └── SceneManager.java
├── auction-common
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── auction
│                       └── common
│                           ├── dto
│                           │   ├── AuctionDTO.java
│                           │   ├── BidDTO.java
│                           │   ├── ItemDTO.java
│                           │   ├── UserDTO.java
│                           │   └── package-info.java
│                           ├── protocol
│                           │   ├── Message.java
│                           │   └── MessageType.java
│                           └── request
│                               ├── EmptyPayload.java
│                               ├── Requests.java
│                               └── Responses.java
├── auction-server
│   ├── dependency-reduced-pom.xml
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com
│       │           └── auction
│       │               └── server
│       │                   ├── ServerMain.java
│       │                   ├── dao
│       │                   │   ├── AuctionDAO.java
│       │                   │   ├── AutoBidDAO.java
│       │                   │   ├── BidDAO.java
│       │                   │   ├── ItemDAO.java
│       │                   │   ├── UserDAO.java
│       │                   │   └── impl
│       │                   │       ├── SQLiteAuctionDAO.java
│       │                   │       ├── SQLiteAutoBidDAO.java
│       │                   │       ├── SQLiteBidDAO.java
│       │                   │       ├── SQLiteItemDAO.java
│       │                   │       └── SQLiteUserDAO.java
│       │                   ├── db
│       │                   │   └── DatabaseManager.java
│       │                   ├── exception
│       │                   │   ├── AuctionException.java
│       │                   │   ├── AuthException.java
│       │                   │   └── BidException.java
│       │                   ├── factory
│       │                   │   ├── ItemFactory.java
│       │                   │   └── UserFactory.java
│       │                   ├── model
│       │                   │   ├── Admin.java
│       │                   │   ├── Art.java
│       │                   │   ├── Auction.java
│       │                   │   ├── AuctionStatus.java
│       │                   │   ├── AutoBid.java
│       │                   │   ├── BidTransaction.java
│       │                   │   ├── Bidder.java
│       │                   │   ├── Electronics.java
│       │                   │   ├── Entity.java
│       │                   │   ├── Item.java
│       │                   │   ├── ItemCategory.java
│       │                   │   ├── Seller.java
│       │                   │   ├── User.java
│       │                   │   ├── UserRole.java
│       │                   │   └── Vehicle.java
│       │                   ├── network
│       │                   │   ├── AuctionServer.java
│       │                   │   └── ClientHandler.java
│       │                   ├── observer
│       │                   │   ├── AuctionEventBus.java
│       │                   │   └── AuctionObserver.java
│       │                   ├── service
│       │                   │   ├── AuctionService.java
│       │                   │   ├── BidService.java
│       │                   │   ├── ItemService.java
│       │                   │   └── UserService.java
│       │                   └── util
│       │                       ├── DateUtil.java
│       │                       ├── DtoMapper.java
│       │                       └── PasswordUtil.java
│       └── test
│           └── java
│               └── com
│                   └── auction
│                       └── server
│                           ├── service
│                           │   ├── AuctionServiceTest.java
│                           │   ├── BidServiceTest.java
│                           │   └── UserServiceTest.java
│                           └── util
│                               └── PasswordUtilTest.java
├── auction.db
└── pom.xml