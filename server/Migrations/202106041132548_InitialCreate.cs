namespace MoblieSecServerTest.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class InitialCreate : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.Messages",
                c => new
                    {
                        msgId = c.Int(nullable: false, identity: true),
                        userMsgId = c.Int(nullable: false),
                        msgRead = c.Boolean(nullable: false),
                        text = c.String(),
                        userPhone = c.String(),
                    })
                .PrimaryKey(t => t.msgId);
            
        }
        
        public override void Down()
        {
            DropTable("dbo.Messages");
        }
    }
}
