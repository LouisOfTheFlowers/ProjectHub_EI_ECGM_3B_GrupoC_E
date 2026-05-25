-- Ensure app registrations always have a matching public.users profile.
-- This also repairs accounts that already exist in auth.users but are missing from public.users.

create or replace function public.user_ensure_own_profile(
  p_nome text,
  p_username text
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  current_email text;
  current_status text;
begin
  current_email := auth.jwt() ->> 'email';

  if current_email is null or length(trim(current_email)) = 0 then
    raise exception 'Utilizador autenticado sem email.';
  end if;

  select
    case
      when au.email_confirmed_at is null then 'PENDENTE'
      else 'ATIVO'
    end
  into current_status
  from auth.users au
  where lower(au.email) = lower(current_email)
  limit 1;

  insert into public.users (
    nome,
    username,
    email,
    password,
    role,
    status
  )
  values (
    coalesce(nullif(trim(p_nome), ''), split_part(current_email, '@', 1)),
    coalesce(nullif(trim(p_username), ''), split_part(current_email, '@', 1)),
    current_email,
    'SUPABASE_AUTH',
    'UTILIZADOR',
    coalesce(current_status, 'ATIVO')
  )
  on conflict (email) do update
  set
    password = 'SUPABASE_AUTH';
end;
$$;

grant execute on function public.user_ensure_own_profile(text, text) to authenticated;

create or replace function public.handle_auth_user_profile()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.users (
    nome,
    username,
    email,
    password,
    role,
    status
  )
  values (
    coalesce(new.raw_user_meta_data ->> 'nome', split_part(new.email, '@', 1)),
    coalesce(new.raw_user_meta_data ->> 'username', split_part(new.email, '@', 1)),
    new.email,
    'SUPABASE_AUTH',
    'UTILIZADOR',
    case
      when new.email_confirmed_at is null then 'PENDENTE'
      else 'ATIVO'
    end
  )
  on conflict (email) do update
  set
    password = 'SUPABASE_AUTH',
    status = case
      when new.email_confirmed_at is null then public.users.status
      else 'ATIVO'
    end;

  return new;
end;
$$;

drop trigger if exists on_auth_user_created_or_updated on auth.users;

create trigger on_auth_user_created_or_updated
after insert or update of email_confirmed_at, raw_user_meta_data
on auth.users
for each row
execute function public.handle_auth_user_profile();
