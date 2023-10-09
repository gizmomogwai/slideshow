# coding: utf-8
require "English"

#SSH_TARGET = "pi@slideshow.local"
#SSH_TARGET = "pi@seehaus.local"

SSH_TARGETS = [
  "pi@slideshow.local",
  "pi@seehaus-blau.local",
]

TARGET_PATH = "/home/pi/"

desc "Build"
task :build do
  sh "./gradlew --watch-fs build"
end


#desc "Deploy images to target"
#task :deploy_images do
#  ["2013/06", "2012/06"].each do |path|
#   year = path.split("/").first
#    sh "ssh #{SSH_TARGET} mkdir -p #{TARGET_PATH}/#{year}"
#    sh "scp -r ~/Pictures/ImageLib/#{path} #{SSH_TARGET}:#{TARGET_PATH}/#{year}"
#  end
#end


def server
  "qnappi"
end

require 'date'
def reference
  DateTime.new(2001, 01, 01)
end

class Image
  attr_reader :path, :created, :added
  def initialize(path, orientation, created, added)
    @path = path
    @orientation = orientation

    @created = reference + created.to_i.seconds
    @added = reference + added.to_i.seconds
  end
  def from_renders(photos_library)
    ext = File.extname(@path)
    pattern = "#{photos_library}/resources/renders/#{@path.gsub(ext, '')}*#{ext}"
    rendered = Dir.glob(pattern)
    p = if rendered.size > 1
      raise "Dont know which rendered to take #{rendered.join(', ')}"
    elsif rendered.size == 1
      rendered.first
    else
      nil
    end
  end
  def from_originals(photos_library)
    File.join(photos_library, "originals", @path)
  end
  def normalize(photos_library, tmp_file)
    p = from_renders(photos_library) || from_originals(photos_library)
    if heic?(@path)
      converted = "#{tmp_file}-converted.jpg"
      cmd = "convert \"#{p}\" \"#{converted}\""
      res = system cmd
      raise "Cannot run #{cmd}" unless res
      cmd = "mv \"#{converted}\" \"#{tmp_file}\""
      res = system cmd
      raise "Cannot run #{cmd}" unless res
      return tmp_file
    end
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
    res = system(command)
    raise "Cannot execute #{command}" unless res

    return tmp_file
  end

  JPG_PATTERN = Regexp.new(".*(jpg|jpeg|png)", Regexp::IGNORECASE)
  HEIC_PATTERN = Regexp.new(".*heic", Regexp::IGNORECASE)
  def supported_format?
    heic?(@path) || jpg?(@path)
  end
  def heic?(path)
    HEIC_PATTERN.match(path)
  end
  def jpg?(path)
    JPG_PATTERN.match(path)
  end
  def to_s
    "Image #{@path} #{@created} #{@added} #{@orientation}"
  end
  def path_on_server
    "%04d/%02d/%04d-%02d-%02d/%s" % [@created.year, @created.month, @created.year, @created.month, @created.day,
      @path
        .gsub("/", "")
        .gsub(" ", "")
        .gsub(".heic", ".jpg")]
  end
end # class Image


def parse(lines)
  lines
    .split("\n")
    .map{ |line| line.split("|") }
    .map{ |path, filename, orientation, created, added| Image.new(File.join(path, filename), orientation, created, added) }
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

class Numeric
  def minutes; self/1440.0 end
  alias :minute :minutes

  def seconds; self/86400.0 end
  alias :second :seconds
end

desc "Copy images to slideshow server"
task :copy_images_to_slideshow do
  home = ENV["HOME"]
  database_file = "Photos.sqlite"
  photos_lib = "#{home}/Pictures/Photos Library.photoslibrary"
  sh "cp \"#{photos_lib}/database/#{database_file}\" #{home}/tmp/"
  puts "copy done"
  command = "sqlite3 #{home}/tmp/Photos.sqlite \"select ZDirectory,ZFileName,ZOrientation,ZDateCreated,ZAddedDate from ZGENERICASSET where ZFAVORITE=1;\""
  output = `#{command}`
  if not $?.success?
    raise "Cannot run #{command}"
  end

  images = parse(output)
  images = images.filter{|image|image.supported_format?()}
  done = load_done()
  images.each do |image|
    if done.include?(image.path)
      puts "already transferred #{image.path}"
      next
    end
    begin
      local_file = image.normalize(photos_lib, "/tmp/transformed")

      path = File.dirname(image.path_on_server)
      ssh_path = path.gsub(" ", "-")

      target_path = "#{ENV['HOME']}/Sync/Slideshow/#{ssh_path}/#{File.basename(image.path_on_server)}"
#      sh "ssh #{server} mkdir -p #{server_path}"
      #      sh "scp -r \"#{local_file}\" \"#{server}:'#{server_path}/#{File.basename(image.path_on_server)}'\""
      sh "mkdir -p \"#{File.dirname(target_path)}\""
      sh "cp \"#{local_file}\" \"#{target_path}\""
      done[image.path] = true
      save_done(done)

#      p = File.join("tmp", "ttt", path)
#      sh "mkdir -p #{p}"
#      cmd = "cp \"#{local_file}\" \"#{File.join(p, File.basename(image.path_on_server))}\""
#      sh cmd
    rescue => e
      puts e
      puts e.backtrace
    end
  end
end

SSH_TARGETS.each do |target|
  namespace target do
    desc "Prepare slideshow server on #{target}"
    task :prepare do
      sh "scp -r src/systemd/.config/systemd #{target}:.config/"
      sh "ssh #{target} systemctl --user daemon-reload"
    end

    desc "Deploy slideshow to #{target}"
    task :deploy => [:build] do
      sh "ssh #{target} mkdir -p #{TARGET_PATH}"
      sh "ls -lha build/libs/slideshow-all.jar"
      sh "scp build/libs/slideshow-all.jar #{target}:#{TARGET_PATH}"
      sh "ssh #{target} touch #{TARGET_PATH}/slideshow-all.jar-updated"
    end

    desc "show and get server logs from #{target}"
    task :show_and_get_server_logs do
      sh "ssh #{target} journalctl --user-unit=slideshow | tee logs.txt"
    end

    desc "restart server on #{target}"
    task :restart_server do
      sh "ssh #{target} systemctl restart --user slideshow"
    end

    desc "Vaccum logs on #{target}"
    task :vaccum do
      sh "ssh #{target} journalctl --vacuum-time=8h"
    end
  end
end
