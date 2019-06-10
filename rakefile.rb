SSH_TARGET = "pi@192.168.0.140"

TARGET_PATH = "/home/pi/Pictures/ImageLib"

desc "Deploy slideshow to target"
task :deploy do
  sh "./gradlew build"
  sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}"
  sh "scp build/libs/slideshow-all.jar #{SSH_TARGET}:#{TARGET_PATH}"
end

desc "Deploy images to target"
task :deploy_images do
  ["2013/06", "2012/06"].each do |path|
    year = path.split("/").first
    sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}/#{year}"
    sh "scp -r ~/Pictures/ImageLib/#{path} #{SSH_TARGET}:#{TARGET_PATH}/#{year}"
  end
end


def server
  "qnappi"
end

JPG_PATTERN = Regexp.new(".*(jpg|jpeg|png)", Regexp::IGNORECASE)
def jpg?(file)
  JPG_PATTERN.match(file)
end

desc "Copy images to slideshow server"
task :copy_images_to_slideshow do
  photos_lib = "/Users/monica/Pictures/Photos Library.photoslibrary"
  sh "cp \"#{photos_lib}/database/photos.db\" tmp/"
  command = 'sqlite3 tmp/photos.db "select imagePath from RKVersion inner join RKMaster where RKVersion.isFavorite = 1 and RKMaster.uuid = RKVersion.masterUuid;"'
  output = `#{command}`
  if not $?.success?
    raise "Cannot run #{command}"
  end

  masters = "#{photos_lib}/Masters"
  lines = output.split("\n")
  puts "#{lines.count} images alltogether"
  lines = lines.filter{|file|jpg?(file)}
  puts "#{lines.count} jpg/jpeg/png images"
  
  lines.each do |file|
    
    path = File.dirname(file)
    ssh_path = path.gsub(" ", "-")

    server_path = "/share/Qmultimedia/Slideshow/#{ssh_path}"
    maverick_path = "#{masters}/#{file}"
    if File.exist?(maverick_path)
      sh "ssh #{server} mkdir -p #{server_path}"
      sh "scp -r \"#{maverick_path}\" '#{server}:#{server_path}'"
    else
      puts "Missing file on maverick: #{maverick_path}"
    end
  end
  sh 'rm tmp/photos.db'
end


task :default => [:deploy]
