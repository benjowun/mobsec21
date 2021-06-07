namespace MoblieSecServerTest.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddTextByteArray : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Messages", "text", c => c.Binary());
        }
        
        public override void Down()
        {
            DropColumn("dbo.Messages", "text");
        }
    }
}
