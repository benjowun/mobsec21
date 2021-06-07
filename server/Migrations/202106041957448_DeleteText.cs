namespace MoblieSecServerTest.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class DeleteText : DbMigration
    {
        public override void Up()
        {
            DropColumn("dbo.Messages", "text");
        }
        
        public override void Down()
        {
            AddColumn("dbo.Messages", "text", c => c.String());
        }
    }
}
