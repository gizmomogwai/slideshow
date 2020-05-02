# coding: utf-8
SSH_TARGET = "pi@slideshow"

TARGET_PATH = "/home/pi/Pictures/ImageLib"

desc "Build"
task :build do
  sh "./gradlew build"
end

desc "Deploy slideshow to target"
task :deploy => [:build] do
  sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}"
  sh "ls -lha build/libs/slideshow-all.jar"
  sh "scp build/libs/slideshow-all.jar #{SSH_TARGET}:#{TARGET_PATH}"
  sh "ssh #{SSH_TARGET} touch #{TARGET_PATH}/slideshow-all.jar-updated"
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

class Image
  attr_reader :path
  def initialize(path, orientation)
    @path = path
    @orientation = orientation
  end
  def normalize(base, tmp_file)
    p = File.join(base, @path)
    
    command =
      case @orientation
      when "1"
        "cp \"#{p}\" #{tmp_file}"
      when "8"
        "jpegtran -rotate 270 \"#{p}\" > #{tmp_file}"
      when "3"
        "jpegtran -rotate 180 \"#{p}\" > #{tmp_file}"
      when "6"
        "jpegtran -rotate 90 \"#{p}\" > #{tmp_file}"
      else
        raise "Cannot handle #{@orientation} for #{p}"
      end
    puts(command)
    res = system(command)
    if res == false
      raise "Cannot execute command"
    end
    tmp_file
  end
  JPG_PATTERN = Regexp.new(".*(jpg|jpeg|png)", Regexp::IGNORECASE)
  def jpg?
    JPG_PATTERN.match(@path)
  end
end


def parse(lines)
  lines
    .split("\n")
    .map{ |line| line.split("|") }
    .map{ |path, orientation| Image.new(path, orientation) }
end

require 'yaml'
def load_done
  if File.exist?("done.yaml")
    return YAML.load(File.read("done.yaml"))
  else
    return {}
  end
end

def save_done(done)
  File.open("done.yaml", "w") do |io|
    io.puts(done.to_yaml)
  end
end

desc "Copy images to slideshow server"
task :copy_images_to_slideshow do
  home = ENV["HOME"]
  photos_lib = "#{home}/Pictures/Photos Library.photoslibrary"
  sh "cp \"#{photos_lib}/database/photos.db\" #{home}/tmp/"
  puts "copy done"
  command = "sqlite3 #{home}/tmp/photos.db \"select imagePath,RKVersion.orientation,RKVersion.isFavorite from RKVersion inner join RKMaster where RKVersion.isFavorite = 1 and RKMaster.uuid = RKVersion.masterUuid;\""
  output = `#{command}`
  puts "command done ->\n#{output}"
  if not $?.success?
    raise "Cannot run #{command}"
  end

  masters = "#{photos_lib}/Masters"
  images = parse(output)
  puts "#{images.count} images alltogether"
  images = images.filter{|image|image.jpg?()}
  puts "#{images.count} jpg/jpeg/png images"

  done = load_done()
  images.each do |image|
    if done.include?(image.path)
      puts "already transferred #{image.path}"
      next
    end
    
    begin
      local_file = image.normalize(masters, "/tmp/transformed")
      
      path = File.dirname(image.path)
      ssh_path = path.gsub(" ", "-")

      server_path = "/share/Qmultimedia/Slideshow/#{ssh_path}"
      sh "ssh #{server} mkdir -p #{server_path}"
      sh "scp -r \"#{local_file}\" \"#{server}:'#{server_path}/#{File.basename(image.path)}'\""

      done[image.path] = true
      save_done(done)
    rescue => e
      puts e
      puts e.backtrace
    end
  end
  sh "rm #{home}/tmp/photos.db"
end

desc 'show and get server logs'
task :show_and_get_server_logs do
  sh "ssh #{SSH_TARGET} journalctl --user-unit=slideshow | tee logs.txt"
end
task :default => [:deploy]

desc "restart server"
task :restart_server do
  sh "ssh #{SSH_TARGET} systemctl restart --user slideshow"
end

desc "Vaccum logs on the server"
task :vaccum do
  sh "ssh #{SSH_TARGET} journalctl --vacuum-time=8h"
end
