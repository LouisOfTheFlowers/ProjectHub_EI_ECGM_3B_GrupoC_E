create or replace function public.notify_project_membership_created()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  project_name text;
begin
  select p.nome
  into project_name
  from public.projetos p
  where p.id = new.projeto_id;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    new.user_id,
    'Novo projeto',
    'Foste adicionado ao projeto "' || coalesce(project_name, 'sem nome') || '".',
    'PROJECT',
    'user/projects',
    new.projeto_id
  );

  return new;
end;
$$;

drop trigger if exists notify_project_membership_created_trigger on public.projeto_users;

create trigger notify_project_membership_created_trigger
after insert on public.projeto_users
for each row
execute function public.notify_project_membership_created();
