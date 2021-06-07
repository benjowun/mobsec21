using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;

namespace MoblieSecServerTest
{
    public class myModel : DbContext
    {
        // Der Kontext wurde für die Verwendung einer myModel-Verbindungszeichenfolge aus der 
        // Konfigurationsdatei ('App.config' oder 'Web.config') der Anwendung konfiguriert. Diese Verbindungszeichenfolge hat standardmäßig die 
        // Datenbank 'MoblieSecServerTest.myModel' auf der LocalDb-Instanz als Ziel. 
        // 
        // Wenn Sie eine andere Datenbank und/oder einen anderen Anbieter als Ziel verwenden möchten, ändern Sie die myModel-Zeichenfolge 
        // in der Anwendungskonfigurationsdatei.
        public myModel() : base("name=myModel")
        {

        }

        // Fügen Sie ein 'DbSet' für jeden Entitätstyp hinzu, den Sie in das Modell einschließen möchten. Weitere Informationen 
        // zum Konfigurieren und Verwenden eines Code First-Modells finden Sie unter 'http://go.microsoft.com/fwlink/?LinkId=390109'.

        //public System.Data.Entity.DbSet<MobileSecServer.Models.User> Users { get; set; }
        public System.Data.Entity.DbSet<MoblieSecServerTest.Models.Message> Messages { get; set; }
    }

    //public class MyEntity
    //{
    //    public int Id { get; set; }
    //    public string Name { get; set; }
    //}
}